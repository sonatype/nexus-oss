#!/usr/bin/perl

# this script requires the following dependencies, which you can install from CPAN:
#
# cpanp install Net::SMTP::SSL MIME::Base64 Authen::SASL Email::Sender Getopt::Flex Net::OSCAR
# or
# cpanm Net::SMTP::SSL MIME::Base64 Authen::SASL Email::Sender Getopt::Flex Net::OSCAR
#


use strict;
use warnings;

use Getopt::Flex;
use Net::OSCAR qw(:standard);
use Email::Simple;
use Email::Sender::Simple qw(sendmail);
use Email::Sender::Transport::SMTP;
 
my (@notify_aim, @notify_email);
my ($aim_screen_name, $aim_password);
my ($smtp_host, $smtp_port, $smtp_user, $smtp_password, $smtp_ssl, $email_from, $strip_pass); 

my $subject = '';

my $op = Getopt::Flex->new({
    spec         => {
        'notify-aim'        => { type => 'ArrayRef[Str]', var => \@notify_aim, desc => 'The AIM screename to send the input to' },
        'notify-email'      => { type => 'ArrayRef[Str]', var => \@notify_email, desc => 'The email address to send the input to' },
        'aim-screen-name'   => { type => 'Str', var => \$aim_screen_name, desc => 'The AIM screename to use for signing into AIM server' },
        'aim-password'      => { type => 'Str', var => \$aim_password, desc => 'The AIM password to use for signing into AIM server' },
        'email-from'        => { type => 'Str', var => \$email_from, desc => 'The `from` value for emails' },
        'subject'           => { type => 'Str', var => \$subject, desc => 'The subject of the input (will be used as subject in emails or prepended in AIM)' },
        'smtp-host'         => { type => 'Str', var => \$smtp_host, desc => 'The host of SMTP server' },
        'smtp-port'         => { type => 'Str', var => \$smtp_port, desc => 'The port of SMTP server' },
        'smtp-user'         => { type => 'Str', var => \$smtp_user, desc => 'The username to use for authentication in SMTP server' },
        'smtp-password'     => { type => 'Str', var => \$smtp_password, desc => 'The password to use for authentication in SMTP server' },
        'smtp-ssl'          => { type => 'Bool', var => \$smtp_ssl, desc => 'When provided, script will use SSL for connection to SMTP' },
        'strip-pass-info'   => { type => 'Bool', var => \$strip_pass, desc => 'When provided, script will strip the [PASS] lines from the message' },
    }, 
    config       => {
        'usage'         => 'notify.pl OPTIONS',
        'desc'          => 'Redirect the input to AIM and/or Email',
        'auto_help'     => 1,
    }
});

$op->getopts();

my $input   = do { local $/; <STDIN> };
$subject    = ($subject || "Notification script") . "";

# TODO - replace ANSI colors with HTML coloring?
$input      =~ s/\033\[\d+m(.*?)\033\[\d+m/$1/gs;

if ($strip_pass) {
    $input      =~ s/\[PASS\].*?\n//gms;	
    $input      =~ s/TODO:.*?\n//gms;
}

# redirect to AIM
if (@notify_aim) {
	die 'Missing `aim-screen-name` option, required when `notify-aim` is given' unless $aim_screen_name;
	die 'Missing `aim-password` option, required when `notify-aim` is given' unless $aim_password;
	
    my $oscar       = Net::OSCAR->new();
    my $is_done     = 0;
    
    my $on_signon_done = sub {
    	my ($oscar) = @_;
    	
    	sleep(1);
    	
    	foreach my $scree_name (@notify_aim) {
    	   $oscar->send_im($notify_aim[ 0 ], $input);
    	   sleep(1);	
    	}
    	
    	$is_done = 1;
    };
    
    my $on_error = sub {
    	my ($oscar, $connection, $error, $description, $fatal) = @_;
    	
    	print STDERR "AIM ERROR: $description";
    	
    	$is_done = 1 if $fatal;
    };
    
    $oscar->set_callback_signon_done($on_signon_done);
    $oscar->set_callback_snac_unknown(sub {});
    
    $oscar->signon(
        screenname          => $aim_screen_name,
        password            => $aim_password
    );
    
    while (!$is_done) {
        $oscar->do_one_loop();
    }
    
    $oscar->signoff();
}

if (@notify_email) {
    my $email = Email::Simple->create(
        header => [
            From    => $email_from ? "\"Siesta Automation Server\" <$email_from>" : "\"Siesta Automation Server\" <siesta\@bryntum.com>",
            Subject => $subject,
        ],
        body => $input,
    );
    
    my $transport = Email::Sender::Transport::SMTP->new({
        host            => $smtp_host,
        ( $smtp_port ? (port => $smtp_port) : () ),
        sasl_username   => $smtp_user,
        sasl_password   => $smtp_password,
        ssl             => $smtp_ssl
    });    
    
    sendmail($email, {
    	to             => \@notify_email,
    	transport      => $transport
    });	
}
